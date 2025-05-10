import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DurationCalculationExample {
    public static void main(String[] args) {
        // 创建 Duration 对象
        Duration duration1 = Duration.ofSeconds(9);
        Duration duration2 = Duration.ofMinutes(10);
        Duration duration3 = Duration.parse("PT1H30M");

        // 计算 Duration 对象
        Duration sum = duration1.plus(duration2);
        Duration difference = duration3.minus(duration1);

        // 获取 Duration 的值
        long minutes = duration3.toMinutes();

        // 计算两个 Instant 之间的 Duration
        Instant startTime = Instant.now().minus(1, ChronoUnit.MINUTES);
        Instant endTime = Instant.now();
        Duration between = Duration.between(startTime, endTime);

        // 输出结果
        System.out.println("duration1: " + duration1);
        System.out.println("duration2: " + duration2);
        System.out.println("duration3: " + duration3);
        System.out.println("sum: " + sum);
        System.out.println("difference: " + difference);
        System.out.println("minutes: " + minutes);
        System.out.println("between: " + between);
    }
}