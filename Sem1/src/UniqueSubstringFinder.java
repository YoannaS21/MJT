public class UniqueSubstringFinder {
    public static String longestUniqueSubstring(String s) {
        char[] str = s.toCharArray();
        String result = "";

        for (int i = 0; i < str.length; i++) {
            boolean[] letters = new boolean[26];
            StringBuilder curr = new StringBuilder();
            for (int j = i; j < str.length; j++) {
                if (letters[(int) str[j] - (int) 'a']) {
                    break;
                } else {
                    curr.append(str[j]);
                    letters[(int) str[j] - (int) 'a'] = true;
                }
            }
            if (result.length() < curr.length()) {
                result = curr.toString();
            }
        }
        return result;
    }

}
