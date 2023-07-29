import java.sql.Connection;

interface LogInInterface {
    Connection dbConnect();
    void init();
    void close();
    void exit();
    void btnLogIn();
}

//interface (not gui) is still not finalize, still confused about this interface (not gui)