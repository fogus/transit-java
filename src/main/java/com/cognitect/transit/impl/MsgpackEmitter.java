// Copyright (c) Cognitect, Inc.
// All rights reserved.

package com.cognitect.transit.impl;

import com.cognitect.transit.Writer;
import com.cognitect.transit.Handler;
import org.apache.commons.codec.binary.Base64;
import org.msgpack.packer.Packer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MsgpackEmitter extends AbstractEmitter {

    private final Packer gen;

    public MsgpackEmitter(Packer gen, Map<Class, Handler> handlers) {
        super(handlers);
        this.gen = gen;
    }

    @Override
    public void emit(Object o, boolean asMapKey, WriteCache cache) throws Exception {
        marshalTop(o, cache);
    }

    @Override
    public void emitNil(boolean asMapKey, WriteCache cache) throws Exception {
        this.gen.writeNil();
    }

    @Override
    public void emitString(String prefix, String tag, String s, boolean asMapKey, WriteCache cache) throws Exception {

        StringBuilder sb = new StringBuilder();
        if(prefix != null)
            sb.append(prefix);
        if(tag != null)
            sb.append(tag);
        if(s != null)
            sb.append(s);

        String outString = cache.cacheWrite(sb.toString(), asMapKey);

        if(asMapKey)
            this.gen.write(outString);
        else
            this.gen.write(outString);
    }

    @Override
    public void emitBoolean(Boolean b, boolean asMapKey, WriteCache cache) throws Exception {
        this.gen.write(b);
    }

    @Override
    public void emitInteger(Object o, boolean asMapKey, WriteCache cache) throws Exception {
        // TODO: BigInteger?

        if (o instanceof String) this.emitString(Writer.ESC_STR, "i", o.toString(), asMapKey, cache);

        long i = Util.numberToPrimitiveLong(o);

        if ((i > Long.MAX_VALUE) || (i < Long.MIN_VALUE))
            this.emitString(Writer.ESC_STR, "i", o.toString(), asMapKey, cache);

        this.gen.write(i);
    }

    @Override
    public void emitDouble(Object d, boolean asMapKey, WriteCache cache) throws Exception {
        if (d instanceof Double)
            this.gen.write((Double) d);
        else if (d instanceof Float)
            this.gen.write((Float) d);
        else
            throw new Exception("Unknown floating point type: " + d.getClass());
    }

    @Override
    public void emitBinary(Object b, boolean asMapKey, WriteCache cache) throws Exception {
        byte[] encodedBytes = Base64.encodeBase64((byte[])b);
        emitString(Writer.ESC_STR, "b", new String(encodedBytes), asMapKey, cache);
    }

    @Override
    public void emitQuoted(Object o, WriteCache cache) throws Exception {

        emitMapStart(1L);
        emitString(Writer.ESC_TAG, "'", null, true, cache);
        marshal(o, false, cache);
        emitMapEnd();
    }

    @Override
    public long arraySize(Object a) {
        if(a instanceof List)
            return ((List)a).size();
        else if (a.getClass().isArray())
            return Array.getLength(a);
        else if (a instanceof Iterable) {
            int i = 0;
            for (Object o : (Iterable) a) {
                i++;
            }
            return i;
        }
        else
            throw new UnsupportedOperationException("arraySize not supported on this type " + a.getClass().getSimpleName());

    }

    @Override
    public void emitArrayStart(Long size) throws Exception {
        this.gen.writeArrayBegin(size.intValue());
    }

    @Override
    public void emitArrayEnd() throws Exception {
        this.gen.writeArrayEnd();
    }

    @Override
    public long mapSize(Object m) {
        if(m instanceof Collection)
            return ((Collection) m).size();
        else
            throw new UnsupportedOperationException("mapSize not supported on this type " + m.getClass().getSimpleName());
    }

    @Override
    public void emitMapStart(Long size) throws Exception {
        this.gen.writeMapBegin(size.intValue());
    }

    @Override
    public void emitMapEnd() throws Exception {
        this.gen.writeMapEnd();
    }

    @Override
    public void flushWriter() throws IOException {
        this.gen.flush();
    }

    @Override
    public boolean prefersStrings() {
        return true;
    }
}
