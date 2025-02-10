package com.zhou.goldtask.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HtmlController {
    @GetMapping("/login")
    public String login(@CookieValue(value = "username", defaultValue = "") String username) {
        if (username == null || "".equals(username)) {
            return "login";
        }
        return "redirect:/home"; // 返回登录页面
    }


    @GetMapping("/setCookie")
    public String doLogin(String username, HttpServletResponse response) {
        // 这里添加你的登录逻辑，例如验证用户名和密码
        // 假设登录成功，设置Cookie
        Cookie cookie = new Cookie("username", username);
        cookie.setSecure(true);
        cookie.setMaxAge(60 * 60 * 24); // Cookie有效期一天
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                String.format("%s=%s; HttpOnly; Secure; SameSite=None",
                        cookie.getName(), cookie.getValue()));
        return "redirect:/home"; // 登录成功后重定向到主页
    }

    @GetMapping("/home")
    public String home(@CookieValue(value = "username", defaultValue = "") String username, Model model) {
        if (username == null || "".equals(username)) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "home"; // 返回主页
    }

    // 清理 Cookie 并跳转到首页
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("username", null);
        cookie.setMaxAge(0);  // 删除 Cookie
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                String.format("%s=%s; HttpOnly; Secure; SameSite=None",
                        cookie.getName(), cookie.getValue()));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        return "redirect:/login";
    }
}