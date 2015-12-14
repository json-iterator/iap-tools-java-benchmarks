package com.jenkov.iap.ion.write;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jjenkov on 07-11-2015.
 */
public class IapDoubleWriteBenchmark {

    public static class SingleFieldPojo {
        public double field0 = 1.1D;
    }

    public static class MultiFieldPojo {
        public double field0   = 1.1D;
        public double field1   = 12.12D;
        public double field2   = 123.123D;
        public double field3   = 1234.1234D;
        public double field4   = 12345.12345D;
        public double fieldxx5 = 123456.1D;             //xx to make the field name longer than 7 chars - to force longer key field (instead of compact key field)
        public double fieldxx6 = 1234567.12D;
        public double fieldxx7 = 12345678.123D;
        public double fieldxx8 = 123456789.1234D;
        public double fieldxx9 = 1234567890.12345D;
    }



    @State(Scope.Thread)
    public static class IapState {
        SingleFieldPojo singleFieldPojo = new SingleFieldPojo();
        MultiFieldPojo  multiFieldPojo = new MultiFieldPojo();

        IonObjectWriter writerSingle = new IonObjectWriter(SingleFieldPojo.class);
        IonObjectWriter writerMulti  = new IonObjectWriter(MultiFieldPojo.class);

        byte[]    dest   = new byte[10 * 1024];
    }

    @State(Scope.Thread)
    public static class JacksonState {
        SingleFieldPojo singleFieldPojo = new SingleFieldPojo();
        MultiFieldPojo multiFieldPojo = new MultiFieldPojo();

        ObjectMapper objectMapper = new ObjectMapper();

        ByteArrayOutputStream out = new ByteArrayOutputStream(10 * 1024);

        @Setup(Level.Invocation)
        public void doSetup() {
            out.reset();
        }

    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object iapWriteSingle(IapState state, Blackhole blackhole) {

        state.writerSingle.writeObject(state.singleFieldPojo, 2, state.dest, 0);

        blackhole.consume(state.dest);
        return state.dest;
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object iapWriteMulti(IapState state, Blackhole blackhole) {

        state.writerMulti.writeObject(state.multiFieldPojo, 2, state.dest, 0);

        blackhole.consume(state.dest);
        return state.dest;
    }



    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object jacksonWriteSingle(JacksonState state, Blackhole blackhole) {

        try {
            state.objectMapper.writeValue(state.out, state.singleFieldPojo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object jacksonWriteMulti(JacksonState state, Blackhole blackhole) {

        try {
            state.objectMapper.writeValue(state.out, state.multiFieldPojo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }






}