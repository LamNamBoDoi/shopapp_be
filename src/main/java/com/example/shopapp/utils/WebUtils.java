package com.example.shopapp.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//Lấy đối tượng HttpServletRequest ở bất kỳ đâu trong ứng dụng (dù không ở trong Controller).
public class WebUtils {
    public static HttpServletRequest getCurrentRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
