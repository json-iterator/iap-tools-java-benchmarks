package com.jenkov.iap.ion.write;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.jenkov.iap.ion.IonUtil;
import com.jenkov.iap.ion.pojos.Pojo1Mixed;
import com.jenkov.iap.ion.proto.TestPojo1MixedOuter;
import com.jsoniter.annotation.JsoniterAnnotationSupport;
import com.jsoniter.output.EncodingMode;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.TypeLiteral;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jjenkov on 07-11-2015.
 */
public class IonMixedWriteBenchmark {

    static {
        JsonStream.setMode(EncodingMode.DYNAMIC_MODE);
        JsoniterAnnotationSupport.enable();
    }

    @State(Scope.Thread)
    public static class IapState {
        Pojo1Mixed pojo1Mixed = new Pojo1Mixed();

        IonObjectWriter writer = new IonObjectWriter(Pojo1Mixed.class);

        byte[] field0KeyFieldBytes = null;
        byte[] field1KeyFieldBytes = null;
        byte[] field2KeyFieldBytes = null;
        byte[] field3KeyFieldBytes = null;
        byte[] field4KeyFieldBytes = null;


        byte[]    dest   = new byte[10 * 1024];

        @Setup(Level.Trial)
        public void doSetupOnce() {
            field0KeyFieldBytes = IonUtil.preGenerateKeyField("field0");
            field1KeyFieldBytes = IonUtil.preGenerateKeyField("field1");
            field2KeyFieldBytes = IonUtil.preGenerateKeyField("field2");
            field3KeyFieldBytes = IonUtil.preGenerateKeyField("field3");
            field4KeyFieldBytes = IonUtil.preGenerateKeyField("field4");
        }
    }


    @State(Scope.Thread)
    public static class JacksonState {
        Pojo1Mixed pojo1Mixed = new Pojo1Mixed();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperMsgPack = new ObjectMapper(new MessagePackFactory());
        ObjectMapper objectMapperCbor    = new ObjectMapper(new CBORFactory());
        JsonStream   jsonStream          = new JsonStream(null, 4096);
        TypeLiteral<Pojo1Mixed> pojo1MixedTypeLiteral = TypeLiteral.create(Pojo1Mixed.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream(10 * 1024);

        //byte[] bytes = null;


        @Setup(Level.Trial)
        public void doSetupOnce() {
            try {
                objectMapper.writeValue(out, pojo1Mixed);
                //bytes = out.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Setup(Level.Invocation)
        public void doSetup() {
            out.reset();
        }


    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object ionWriter(IapState state, Blackhole blackhole) {

        int index = 0;
        index += IonWriter.writeObjectBegin(state.dest, index, 1);

        index += IonWriter.writeDirect(state.dest, index, state.field0KeyFieldBytes);
        index += IonWriter.writeBoolean(state.dest, index, state.pojo1Mixed.field0);

        index += IonWriter.writeDirect(state.dest, index, state.field1KeyFieldBytes);
        index += IonWriter.writeInt64(state.dest, index, state.pojo1Mixed.field1);

        index += IonWriter.writeDirect(state.dest, index, state.field2KeyFieldBytes);
        index += IonWriter.writeFloat32(state.dest, index, state.pojo1Mixed.field2);

        index += IonWriter.writeDirect(state.dest, index, state.field3KeyFieldBytes);
        index += IonWriter.writeFloat64(state.dest, index, state.pojo1Mixed.field3);

        index += IonWriter.writeDirect(state.dest, index, state.field4KeyFieldBytes);
        index += IonWriter.writeUtf8(state.dest, index, state.pojo1Mixed.field4);

        IonWriter.writeObjectEnd(state.dest, 0, 1, index -1 -1); //-1 for object lead byte, -1 for length byte

        blackhole.consume(state.dest);
        return state.dest;
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object ionWriterOptimized(IapState state, Blackhole blackhole) {

        int index = 0;
        index += IonWriter.writeObjectBegin(state.dest, index, 1);

        index += IonWriter.writeBoolean(state.dest, index, state.pojo1Mixed.field0);
        index += IonWriter.writeInt64(state.dest, index, state.pojo1Mixed.field1);
        index += IonWriter.writeFloat32(state.dest, index, state.pojo1Mixed.field2);
        index += IonWriter.writeFloat64(state.dest, index, state.pojo1Mixed.field3);
        index += IonWriter.writeUtf8(state.dest, index, state.pojo1Mixed.field4);

        IonWriter.writeObjectEnd(state.dest, 0, 1, index -1 -1); //-1 for object lead byte, -1 for length byte

        blackhole.consume(state.dest);
        return state.dest;
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object ionObjectWriter(IapState state, Blackhole blackhole) {

        state.writer.writeObject(state.pojo1Mixed, 1, state.dest, 0);

        blackhole.consume(state.dest);
        return state.dest;
    }



//    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object jsonWrite(JacksonState state, Blackhole blackhole) {

        try {
            state.objectMapper.writeValue(state.out, state.pojo1Mixed);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object msgPackWrite(JacksonState state, Blackhole blackhole) {

        try {
            state.objectMapperMsgPack.writeValue(state.out, state.pojo1Mixed);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }



//    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object cborWrite(JacksonState state, Blackhole blackhole) {

        try {
            state.objectMapperCbor.writeValue(state.out, state.pojo1Mixed);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }



    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object jsoniterWrite(JacksonState state, Blackhole blackhole) {

        try {
            state.jsonStream.reset(state.out);
            state.jsonStream.writeVal(state.pojo1MixedTypeLiteral, state.pojo1Mixed);
            state.jsonStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }



    @State(Scope.Thread)
    public static class ProtoBufState {
        TestPojo1MixedOuter.TestPojo1Mixed testPojo = null;

        ByteArrayOutputStream out = new ByteArrayOutputStream(10 * 1024);


        @Setup(Level.Trial)
        public void toSetup() {
            testPojo = TestPojo1MixedOuter.TestPojo1Mixed.newBuilder()
                    .setField0(true)
                    .setField1(1234)
                    .setField2(123.12F)
                    .setField3(123456.1234D)
                    .setField4("abcdefg")
                    .build()
            ;
            out.reset();
        }

        @Setup(Level.Invocation)
        public void reset() {
            out.reset();
        }

    }



    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public Object protoBufWrite(ProtoBufState state, Blackhole blackhole) {
        try {
            state.testPojo.writeTo(state.out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blackhole.consume(state.out);
        return state.out;
    }


    public static void main(String[] args) throws IOException, RunnerException {
        Main.main(new String[]{
                "IonMixedWriteBenchmark",
                "-i", "5",
                "-wi", "5",
                "-f", "1",
        });
    }

}
