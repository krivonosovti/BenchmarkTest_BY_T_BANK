package backend.academy;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ReflectionBenchmark {
    private Student student;
    private Method method;
    private MethodHandle methodHandle;
    private java.util.function.Function<Student, String> lambdaFunction;
    private static final String NAME_STR = "name";

    @Setup
    public void setup() throws Throwable {
        student = new Student("Alexander", "Biryukov");

        // Reflection setup
        method = Student.class.getMethod(NAME_STR);

        // MethodHandles setup
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        methodHandle = lookup.findVirtual(Student.class, NAME_STR, MethodType.methodType(String.class));

        // LambdaMetafactory setup
        MethodHandle target = lookup.findVirtual(Student.class, NAME_STR, MethodType.methodType(String.class));
        CallSite callSite = LambdaMetafactory.metafactory(
            lookup,
            "apply",
            MethodType.methodType(java.util.function.Function.class),
            MethodType.methodType(Object.class, Object.class),
            target,
            MethodType.methodType(String.class, Student.class)
        );
        lambdaFunction = (java.util.function.Function<Student, String>) callSite.getTarget().invokeExact();
    }

    @Benchmark
    public void directAccess(Blackhole bh) {
        String name = student.name();
        bh.consume(name);
    }

    @Benchmark
    public void reflection(Blackhole bh) throws Exception {
        String name = (String) method.invoke(student);
        bh.consume(name);
    }

    @Benchmark
    public void methodHandles(Blackhole bh) throws Throwable {
        String name = (String) methodHandle.invoke(student);
        bh.consume(name);
    }

    @Benchmark
    public void lambdaMetafactory(Blackhole bh) {
        String name = lambdaFunction.apply(student);
        bh.consume(name);
    }

    record Student(String name, String surname) {}
}

