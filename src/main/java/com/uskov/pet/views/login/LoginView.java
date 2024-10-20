package com.uskov.pet.views.login;

import com.uskov.pet.security.AuthenticatedUser;
import com.uskov.pet.views.helloworld2.HelloWorld2View;
import com.uskov.pet.webClient.JwtAuthClient;
import com.uskov.pet.webClient.model.AuthRequest;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
@Slf4j
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final JwtAuthClient client;

    public LoginView(AuthenticatedUser authenticatedUser, JwtAuthClient client) {
        this.authenticatedUser = authenticatedUser;
        this.client = client;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("My App");
        i18n.getHeader().setDescription("Login using user/user or admin/admin");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(true);
        setOpened(true);

        addLoginListener(this::onLogin);
    }

    private void onLogin(AbstractLogin.LoginEvent loginEvent) {
        AuthRequest authRequest = new AuthRequest(loginEvent.getUsername(), loginEvent.getPassword());

        UI.getCurrent().getSession().setAttribute("authRequest", authRequest);
        log.info("Auth Request: {}", UI.getCurrent().getSession().getAttribute("authRequest"));

        // Используем JwtAuthClient для запроса токена
        client.login(authRequest).subscribe(
                token -> {
                    UI.getCurrent().access(() -> {
                        UI.getCurrent().getSession().setAttribute("authToken", token);
                        UI.getCurrent().navigate(HelloWorld2View.class);
                    });
                },
                error -> {
                    log.error("Login failed", error);
                    setError(true);  // Показываем ошибку на экране
                }
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
