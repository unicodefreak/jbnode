package net.karmafiles.jbnode;

import org.apache.commons.cli.*;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ilya Brodotsky
 * Date: 02.09.2010
 * Time: 15:51:06
 * <p/>
 * All rights reserved.
 * <p/>
 * Contact me:
 * email, jabber: ilya.brodotsky@gmail.com
 * skype: ilya.brodotsky
 */

public class Bootstrap {

    protected static final String HOST_OPTION = "host";
    protected static final String PORT_OPTION = "port";
    protected static final String MODULES_OPTION = "modules";
    protected static final String HELP_OPTION = "help";
    protected static final String EXECUTABLE_NAME = "jbnode";
    protected static final String JBNODE_XML = "jbnode.xml";
    protected static final String JBNODE_PROPERTIES_XML = "jbnode-properties.xml";

    private Logger logger = Logger.getLogger(Bootstrap.class.getName());

    private JBNode jbNode;

    private void setupAndRun(Properties jbNodeProperties, String[] modules) {
        logger.log(Level.INFO, "Setting up Spring contexts");

        ApplicationContext rootContext = new ClassPathXmlApplicationContext(new String[] {JBNODE_PROPERTIES_XML});

        PropertyPlaceholderConfigurer propertyPlaceholderConfigurer =
                (PropertyPlaceholderConfigurer)rootContext.getBean("jbNodePropertyPlaceholder");
        propertyPlaceholderConfigurer.setProperties(jbNodeProperties);

        ClassPathXmlApplicationContext mainContext = new ClassPathXmlApplicationContext(
                rootContext
        );

        mainContext.addBeanFactoryPostProcessor(propertyPlaceholderConfigurer);
        mainContext.setConfigLocation(JBNODE_XML);
        mainContext.refresh();

        jbNode = (JBNode)mainContext.getBean("jbNode");
        boolean hasErrors = false; 

        for(String module : modules) {
            logger.log(Level.INFO, "Discovering module '" + module + "'");

            ApplicationContext moduleContext = new ClassPathXmlApplicationContext(new String[] { module + ".xml"});

            for(String beanName : moduleContext.getBeanDefinitionNames()) {
                try {
                    Object bean = moduleContext.getBean(beanName);
                    try {
                        if(bean.getClass().getField(Constants.FIELD_JB_NODE_VERSION) != null) {
                            jbNode.configure(beanName, bean);
                        }
                    } catch (NoSuchFieldException e) {
                        
                    }
                } catch (JBNodeException e) {
                    logger.log(Level.SEVERE, "Can't configure bean '" + beanName + "' ", e);
                    hasErrors = true;
                    break;
                }
            }
        }

        if(!hasErrors) {
            try {
                logger.log(Level.INFO, "Starting jbNode...");
                jbNode.start();
            } catch (JBNodeException e) {
                logger.log(Level.SEVERE, "jbNode can't start", e);
            }
        } else {
            logger.log(Level.SEVERE, "Exceptions encountered while configuring jbNode service. Exiting.");
        }

    }

    private void parseAndContinue(String[] args) {

        Option host = OptionBuilder
                .withValueSeparator()
                .hasArgs(1)
                .withDescription("Host (name or ip) to bind to")
                .create(HOST_OPTION);

        Option port = OptionBuilder
                .withValueSeparator()
                .hasArgs(1)
                .withDescription("Port to bind to")
                .create(PORT_OPTION);

        Option modules = OptionBuilder
                .withValueSeparator()
                .hasArgs(1)
                .withDescription("Load modules. Example modules=echo,auth,persistence-db1")
                .create(MODULES_OPTION);

        Option help = new Option(HELP_OPTION, "print command line help");

        Options options = new Options();
        options.addOption(help);
        options.addOption(host);
        options.addOption(port);
        options.addOption(modules);

        CommandLine line;
        CommandLineParser parser = new GnuParser();
        try {
            line = parser.parse( options, args );
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        if(line.hasOption(HELP_OPTION)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(120);
            formatter.printHelp(EXECUTABLE_NAME, options );
            return;
        }

        if(!line.hasOption(HOST_OPTION) || "".equals(line.getOptionValue(HOST_OPTION).trim())) {
            System.err.println("host parameter required");
            return;
        }
        if(!line.hasOption(PORT_OPTION) || "".equals(line.getOptionValue(PORT_OPTION).trim())) {
            System.err.println("port parameter required");
            return;
        }

        String[] modulesArray = new String[]{}; 
        if(line.hasOption(MODULES_OPTION) && !"".equals(line.getOptionValue(MODULES_OPTION).trim())) {
            modulesArray = line.getOptionValue(MODULES_OPTION).split(",");            
        }

        Properties jbNodeProperties = new Properties();
        jbNodeProperties.setProperty(ParameterConstants.JBNODE_HOST_PARAMETER, line.getOptionValue(HOST_OPTION));
        jbNodeProperties.setProperty(ParameterConstants.JBNODE_PORT_PARAMETER, line.getOptionValue(PORT_OPTION));

        setupAndRun(jbNodeProperties, modulesArray);
    }


    public static void main(String[] args) {
        final Bootstrap bootstrap = new Bootstrap();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if(bootstrap.jbNode != null) {
                    bootstrap.jbNode.shutdown();
                }
                bootstrap.logger.log(Level.INFO, "Done.");
            }
        });
        
        bootstrap.parseAndContinue(args);
    }
}
