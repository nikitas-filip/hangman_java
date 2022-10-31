public class ErrorHandling {

     static class InvalidCountException extends Exception {
        public InvalidCountException()
        {
            super("Duplicate words found in the description");
        }

     }

     static class UndersizeException extends Exception {
         public UndersizeException() { super("Dictionary is too small (under 20 words) . Please retry with a different book"); }
     }

    static class InvalidRangeException extends Exception {
        public InvalidRangeException() { super("Words with less than 6 letters exist in the description"); }
    }

    static class UnbalancedException extends Exception {
        public UnbalancedException() { super("Less than 20% of the words in the dictionary has more than 8 letters. Please retry with a different book"); }
    }
}
