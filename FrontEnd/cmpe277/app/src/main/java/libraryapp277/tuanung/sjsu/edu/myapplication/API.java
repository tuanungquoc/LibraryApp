package libraryapp277.tuanung.sjsu.edu.myapplication;

/**
 * Created by VimmiRao on 12/3/2017.
 */

public class API {

        private static final String URL= "http://35.185.86.217:8080";
        public static final String GetBooks = URL+"/api/getbooks";
        public static final String PostBooks = URL+"/api/addbooks";
        public static final String UpdateBooks = URL+"/api/updatebooks";
        public static final String DeleteBooks = URL+"/api/deletebooks";
        public static final String CheckoutBooks = URL+"/api/borrowabook";
        public static final String ReturnBooks = URL+"/api/returns";
        public static final String GetBorrowedBooks = URL+"/api/getBooksBorrowedBy";
        public static final String BookWaitingList = URL+"/api/waitingList";
        public static final String RenewBook = URL+"/api/renew";
        public static final String ReserveBooks = URL + "/api/getBooksReservedBy";
        public static final String GetMyWaitList = URL+"/api/getWaitingListOf";
        public static final String GetBalance = URL +"/api/balance";
        public static final String CronJob = URL + "/api/cronjob";
        public static String registerURL(){
                return URL + "/register";
        }

        public static String logInURL(){
                return URL + "/login";
        }

        public static String confirmURL(){
                return URL + "/confirm";
        }
        public static String resendTokenURL(){
                return URL + "/resendToken";
        }
}
