package bg.sofia.uni.fmi.mjt.jobmatch;

import bg.sofia.uni.fmi.mjt.jobmatch.model.match.SkillRecommendation;

import java.util.Comparator;

public class SkillRecommendationsComparator implements Comparator<SkillRecommendation> {
    @Override
    public int compare(SkillRecommendation o1, SkillRecommendation o2) {
        int cmp = Double.compare(o2.improvementScore(), o1.improvementScore());
        if (cmp != 0) {
            return cmp;
        }
        return o1.skillName().compareTo(o2.skillName());
    }
}
