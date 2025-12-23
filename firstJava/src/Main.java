public class Main {
    void main() {

        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        int size = Integer.MIN_VALUE;
        System.out.println(size);
        boolean value = false;
        System.out.println(value);
        float a = 0.1f;
        System.out.println(a);
        String[] arr; // declaration
        arr = new String[5]; // creation
        arr[0] = "Ivan";
        System.out.println(arr[0]);
        int[] arr2 = {1, 2, 3, 3, 5};

        greet();
        //string are IMMUTABLE
        // for mutable strings
        StringBuilder sb = new StringBuilder("Gossip girl.");
        sb.append("You know you love me ;)");
        //sb.delete(0,6);
        System.out.println(sb);

        String str = new String("The summer i turned pretty");
        str = str.intern();

    }

   void greet() {
        System.out.println("Hello, world!");
    }
}