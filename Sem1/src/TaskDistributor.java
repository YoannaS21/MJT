import java.util.Arrays;

public class TaskDistributor {
    public static int minDifference(int[] tasks)
    {
        Arrays.sort(tasks);
        int sum1 = 0;
        int sum2 = 0;

        for(int i = tasks.length - 1; i >= 0; i--)
        {
            if(sum1 <= sum2)
            {
                sum1 += tasks[i];
            }
            else
            {
                sum2 += tasks[i];
            }
        }
        return Math.abs(sum1 - sum2);
    }
    
}