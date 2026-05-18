package br.com.autospec.backend.core.http;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;

public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public CachedBodyRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            this.body = request.getInputStream().readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getCachedBody() {
        return this.body;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {}
        };
    }
}

