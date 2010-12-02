var sys = require('sys');
var buf = require('buffer');
var jb = require('./jbnode');

/*
    Test composite buffer (check jbnode.js for details).
     
 */
function assert(expression, message) {
    if (!expression) {
        throw new Error("Assertion failed: " + message);
    }
}

var compositeBuffer = new jb.CompositeBuffer();

compositeBuffer.write(new buf.Buffer("0123456789"));
assert(compositeBuffer.readNoAdvance(1).toString() == "0", "__1");
assert(compositeBuffer.readNoAdvance(5).toString() == "01234", "__2");
assert(compositeBuffer.readNoAdvance(10).toString() == "0123456789", "__3");
assert(compositeBuffer.readNoAdvance(0).toString() == "", "__4");

try {
    compositeBuffer.readNoAdvance(11);
    assert(false, "__5");
} catch(e) {};

compositeBuffer.write(new buf.Buffer("0"));
assert(compositeBuffer.readNoAdvance(11).toString() == "01234567890", "__6");
compositeBuffer.write(new buf.Buffer("123456789"));
assert(compositeBuffer.readNoAdvance(20).toString() == "01234567890123456789", "__7");
compositeBuffer.advance(0);
assert(compositeBuffer.readNoAdvance(20).toString() == "01234567890123456789", "__8");
compositeBuffer.advance(1);
assert(compositeBuffer.readNoAdvance(19).toString() == "1234567890123456789", "__9");
compositeBuffer.advance(18);
assert(compositeBuffer.readNoAdvance(1).toString() == "9", "__10");
compositeBuffer.write(new buf.Buffer("a"));
compositeBuffer.write(new buf.Buffer("b"));
compositeBuffer.write(new buf.Buffer("c"));
compositeBuffer.write(new buf.Buffer("d"));
compositeBuffer.write(new buf.Buffer(""));
compositeBuffer.write(new buf.Buffer("e"));
compositeBuffer.write(new buf.Buffer("f"));
assert(compositeBuffer.read(6).toString() == "9abcde", "__11");
assert(compositeBuffer.length == 1, "__12");
assert(compositeBuffer.readNoAdvance(1).toString() == "f", "__12");
assert(compositeBuffer.read(0).toString() == "", "__13");

try {
    compositeBuffer.read(2);
    assert(false, "__14");
} catch(e) {};

assert(compositeBuffer.read(1).toString() == "f", "__15");
assert(compositeBuffer.length == 0, "__16");

try {
    compositeBuffer.write(null);
    assert(false, "__17");
} catch(e) {}

try {
    var dummy;
    compositeBuffer.write(dummy);
    assert(false, "__18");
} catch(e) {} 