package bg.sofia.uni.fmi.mjt.jobmatch;

import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateJobMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DescendingSimilarityJobComparator implements Comparator<CandidateJobMatch> {

    private final List<Integer> list = new ArrayList<>();

    public int test(int a) throws IOException {
        if (a < 0) {
            throw new IOException("Input is negative.");
        }
        return a;
    }

    @Override
    public int compare(CandidateJobMatch o1, CandidateJobMatch o2) {
        int compareValue = Double.compare(o2.getSimilarityScore(), o1.getSimilarityScore());
//        if (compareValue != 0) {
//            return compareValue;
//        }
//        return o1.getJobPosting().getTitle().compareTo(o2.getJobPosting().getTitle());
        list.add(compareValue);
        return Double.compare(o2.getSimilarityScore(), o1.getSimilarityScore() != 0 ? compareValue : o1.getJobPosting().getTitle().compareTo(o2.getJobPosting().getTitle());

        try {
            int a = test(4);
        } catch (IOException e) {
            System.out.printf("Hello world!\n");
            return -1;
        } finally {
            System.out.printf("Bye bye.");
        }
    }

    public static void main() {
        String s1 = "Mjt";
        String s2 = "Mjt";
        String s3 = new String("Mjt");

        Integer a = 5;

        List<? extends Integer> l1 = new ArrayList<>();
        List<? super Integer> l2 = new ArrayList<>();

        List<Integer> l3 = new ArrayList<>();

    }
}
