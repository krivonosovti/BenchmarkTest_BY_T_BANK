package backend.academy;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.experimental.UtilityClass;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@UtilityClass
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int FORKS_AMOUNT = 1;
    private static final int ITERATION_AMOUNT = 5;

    public static void main(String[] args) throws RunnerException, IOException {
        Options options = new OptionsBuilder()
            .include(ReflectionBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.NANOSECONDS)
            .forks(FORKS_AMOUNT)
            .warmupForks(FORKS_AMOUNT)
            .warmupIterations(ITERATION_AMOUNT)
            .warmupTime(TimeValue.seconds(ITERATION_AMOUNT))
            .measurementIterations(ITERATION_AMOUNT)
            .measurementTime(TimeValue.seconds(ITERATION_AMOUNT))
            .build();

        Collection<RunResult> results = new Runner(options).run();
        saveResultsAsMarkdown(results);
    }

    public static void saveResultsAsMarkdown(Collection<RunResult> results) {
        StringBuilder markdown = new StringBuilder("### Финальные результаты бенчмарков\n\n");
        markdown.append("| Способ                  | Среднее время (нс) |\n");
        markdown.append("|-------------------------|--------------------|\n");

        for (RunResult result : results) {
            Result benchmarkResult = result.getPrimaryResult();
            String benchmarkName = (benchmarkResult).getLabel();
            double averageTime = benchmarkResult.getScore();
            markdown.append(String.format("| %-23s | %-18.2f |\n", benchmarkName, averageTime));
        }

        try (FileWriter writer = new FileWriter("benchmark_results.md")) {
            writer.write(markdown.toString());
            LOGGER.log(Level.INFO, "Результаты сохранены в файл benchmark_results.md");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка при сохранении результатов: " + e.getMessage());
        }
    }
}
