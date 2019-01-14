public class User {
    private String username, email, password, book, accounttype, UID;

    User(){
        UID = " ";
        username = " ";
        email = " ";
        password = " ";
        book = " ";
        accounttype = " ";

    }
    User(String username, String email, String password, String book, String accountype){
        this.username = username;
        this.email = email;
        this.password = password;
        this.book = book;
        this.accounttype = accountype;
    }
    User(String email, String password){
        this.email = email;
        this.password = password;

        this.UID = " ";
        this.username = " ";
        this.book = " ";
        this.accounttype = " ";
    }

    public String getAccounttype() {
        return accounttype;
    }

    public String getBook() {
        return book;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getUID() {
        return UID;
    }

    public void setAccounttype(String accounttype) {
        this.accounttype = accounttype;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
