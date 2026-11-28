package computerhardwarestore.model;

import java.time.LocalDate;

public class User {
    private int id;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String username;
    private final String passwordHash;
    private final Role role;

    public User(int id, String firstName, String lastName, LocalDate birthDate,
                String username, String passwordHash, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
}
