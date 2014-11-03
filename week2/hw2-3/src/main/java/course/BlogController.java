package course;


import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import org.apache.commons.lang3.StringEscapeUtils;
import spark.ModelAndView;
import spark.Request;

import javax.servlet.http.Cookie;
import java.net.UnknownHostException;
import java.util.HashMap;

import static spark.Spark.*;

/**
 * Created by tedc on 11/2/14.
 */
public class BlogController {

    private final UserDAO userDAO;
    private final SessionDAO sessionDAO;

    public BlogController(String mongoURIString) throws UnknownHostException {
        final MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURIString));
        final DB blogDatabase = mongoClient.getDB("blog");

        userDAO = new UserDAO(blogDatabase);
        sessionDAO = new SessionDAO(blogDatabase);

        setPort(8082);
        initializeRoutes();
    }

    public static void main(String[] args) throws UnknownHostException {
        if (args.length == 0) {
            new BlogController("mongodb://localhost");
        } else {
            new BlogController(args[0]);
        }
    }

    private void initializeRoutes() {
        get("/", (request, response) -> {
            String username = sessionDAO.findUserNameBySessionId(getSessionCookie(request));

            // src/test/resources/spark/template/freemarker// this is where we would normally load up the blog data
            // but this week, we just display a placeholder.
            HashMap<String, String> root = new HashMap<>();
            return new ModelAndView(root, "blog_template.ftl");
        }, new FreeMarkerEngine());

        get("/welcome", (request, response) -> {
            String cookie = getSessionCookie(request);
            String username = sessionDAO.findUserNameBySessionId(cookie);

            if (username == null) {
                System.out.println("welcome() can't identify the user, redirecting to signup");
                response.redirect("/signup");
            } else {
                ObjectWrapper wrapper
                        = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_21)
                        .build();
                SimpleHash root = new SimpleHash(wrapper);

                root.put("username", username);

                return new FreeMarkerEngine().render(new ModelAndView(root, "welcome.ftl"));
            }
            return "Welcome error";
        });

        get("/signup", (request, response) -> {
            ObjectWrapper wrapper
                    = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_21)
                    .build();
            SimpleHash root = new SimpleHash(wrapper);

            // initialize values for the form.
            root.put("username", "");
            root.put("password", "");
            root.put("email", "");
            root.put("password_error", "");
            root.put("username_error", "");
            root.put("email_error", "");
            root.put("verify_error", "");

            return new ModelAndView(root, "signup.ftl");
        }, new FreeMarkerEngine());

        post("/signup", (request, response) -> {
            String email = request.queryParams("email");
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            String verify = request.queryParams("verify");

            HashMap<String, String> root = new HashMap<>();
            root.put("username", StringEscapeUtils.escapeHtml4(username));
            root.put("email", StringEscapeUtils.escapeHtml4(email));

            if (validateSignup(username, password, verify, email, root)) {
                // good user
                System.out.println("Signup: Creating user with: " + username + " " + password);
                if (!userDAO.addUser(username, password, email)) {
                    // duplicate user
                    root.put("username_error", "Username already in use, Please choose another");
                    return new FreeMarkerEngine().render(new ModelAndView(root, "signup.ftl"));
                } else {
                    // good user, let's start a session
                    String sessionID = sessionDAO.startSession(username);
                    System.out.println("Session ID is" + sessionID);

                    response.raw().addCookie(new Cookie("session", sessionID));
                    response.redirect("/welcome");
                }
            } else {
                // bad signup
                System.out.println("User Registration did not validate");
                return new FreeMarkerEngine().render(new ModelAndView(root, "signup.ftl"));
            }
            return "Signup error";
        });

        get("/login", (request, response) -> {
            ObjectWrapper wrapper
                    = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_21)
                    .build();
            SimpleHash root = new SimpleHash(wrapper);

            // initialize values for the form.
            root.put("username", "");
            root.put("login_error", "");

            return new ModelAndView(root, "login.ftl");
        }, new FreeMarkerEngine());

        get("/logout", (request, response) -> {
            String sessionID = getSessionCookie(request);

            if (sessionID == null) {
                // no session to end
                response.redirect("/login");
            } else {
                // deletes from session table
                sessionDAO.endSession(sessionID);

                // this should delete the cookie
                Cookie c = getSessionCookieActual(request);
                c.setMaxAge(0);

                response.raw().addCookie(c);

                response.redirect("/login");
            }
            return "Logout error";
        });

        post("/login", (request, response) -> {

            String username = request.queryParams("username");
            String password = request.queryParams("password");

            System.out.println("Login: User submitted: " + username + "  " + password);

            DBObject user = userDAO.validateLogin(username, password);

            if (user != null) {

                // valid user, let's log them in
                String sessionID = sessionDAO.startSession(user.get("_id").toString());

                if (sessionID == null) {
                    response.redirect("/internal_error");
                } else {
                    // set the cookie for the user's browser
                    response.raw().addCookie(new Cookie("session", sessionID));
                    response.redirect("/welcome");
                }
            } else {
                ObjectWrapper wrapper
                        = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_21)
                        .build();
                SimpleHash root = new SimpleHash(wrapper);

                root.put("username", StringEscapeUtils.escapeHtml4(username));
                root.put("password", "");
                root.put("login_error", "Invalid Login");
                return new FreeMarkerEngine().render(new ModelAndView(root, "login.ftl"));
            }
            return "login error";
        });


        get("/internal_error", (request, response) -> {
            ObjectWrapper wrapper
                    = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_21)
                    .build();
            SimpleHash root = new SimpleHash(wrapper);
            root.put("error", "System has encountered an error.");
            return new ModelAndView(root, "error_template.ftl");
        }, new FreeMarkerEngine());

    }

    // helper function to get session cookie as string
    private String getSessionCookie(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // helper function to get session cookie as string
    private Cookie getSessionCookieActual(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("session")) {
                return cookie;
            }
        }
        return null;
    }

    // validates that the registration form has been filled out right and username conforms
    public boolean validateSignup(String username, String password, String verify, String email,
                                  HashMap<String, String> errors) {
        String USER_RE = "^[a-zA-Z0-9_-]{3,20}$";
        String PASS_RE = "^.{3,20}$";
        String EMAIL_RE = "^[\\S]+@[\\S]+\\.[\\S]+$";

        errors.put("username_error", "");
        errors.put("password_error", "");
        errors.put("verify_error", "");
        errors.put("email_error", "");

        if (!username.matches(USER_RE)) {
            errors.put("username_error", "invalid username. try just letters and numbers");
            return false;
        }

        if (!password.matches(PASS_RE)) {
            errors.put("password_error", "invalid password.");
            return false;
        }


        if (!password.equals(verify)) {
            errors.put("verify_error", "password must match");
            return false;
        }

        if (!email.equals("")) {
            if (!email.matches(EMAIL_RE)) {
                errors.put("email_error", "Invalid Email Address");
                return false;
            }
        }
        return true;
    }

}
