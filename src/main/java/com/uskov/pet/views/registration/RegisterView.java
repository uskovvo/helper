package com.uskov.pet.views.registration;

import com.uskov.pet.data.Role;
import com.uskov.pet.views.login.LoginView;
import com.uskov.pet.webClient.JwtAuthClient;
import com.uskov.pet.webClient.model.NewUserRequest;
import com.uskov.pet.webClient.model.RegistrationResponse;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@AnonymousAllowed
@PageTitle("Registration")
@Route(value = "register")
public class RegisterView extends Div {

    private TextField username;
    private PasswordField password;
    private EmailField email;
    private Button registerButton;
    private final JwtAuthClient client;

    public RegisterView(JwtAuthClient client) {
        this.client = client;
        getStyle().set("background-color", "var(--lumo-contrast-5pct)")
                .set("display", "flex").set("justify-content", "center")
                .set("padding", "var(--lumo-space-l)");
        FormLayout dialog = new FormLayout();
        dialog.add(getRegistrationForm());
        add(dialog);
    }

    private VerticalLayout getRegistrationForm() {
        username = new TextField("Username");
        password = getPasswordField("Password");
        PasswordField confirmPassword = getPasswordField("Confirm Password");
        email = new EmailField("Email");

        registerButton = new Button("Register");
        registerButton.setDisableOnClick(true);

        registerButton.addClickListener(this::buttonAction);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(username, email, password, confirmPassword, registerButton);

        return verticalLayout;
    }

    private PasswordField getPasswordField(String label) {
        PasswordField passwordField = new PasswordField(label);
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setPattern("^[A-Za-z0-9]+$");
        passwordField.setMinLength(6);
        passwordField.setMaxLength(12);
        passwordField.setI18n(new PasswordField.PasswordFieldI18n()
                .setRequiredErrorMessage("Field is required"));

        if (label.equalsIgnoreCase("password")) {
            passwordField.getI18n()
                    .setMinLengthErrorMessage("Minimum length is 6 characters")
                    .setMaxLengthErrorMessage("Maximum length is 12 characters")
                    .setPatternErrorMessage(
                            "Only letters A-Z and numbers are allowed");
            passwordField.setHelperText(
                    "6 to 12 characters. Only letters A-Z and numbers supported.");
        }

        return passwordField;
    }

    private void buttonAction(ClickEvent<Button> event) {

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADMIN);
        RegistrationResponse response = register(NewUserRequest.builder()
                .username(username.getValue())
                .password(password.getValue())
                .email(email.getValue())
                .roles(roles)
                .build());
        if (response.getStatus().equals(HttpStatusCode.valueOf(200))) {
            Notification.show(response.getMessage());
            registerButton.getUI().ifPresent(ui -> ui.navigate(LoginView.class));

        } else {
            Notification.show(response.getMessage());
        }
    }

    private RegistrationResponse register(NewUserRequest request) {
        try {
            ResponseEntity<String> response = client.register(request).block();
            return new RegistrationResponse(Objects.requireNonNull(response).getStatusCode(), response.getBody());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new RegistrationResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}


