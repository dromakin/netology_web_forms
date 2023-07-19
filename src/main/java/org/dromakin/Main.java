package org.dromakin;

import org.dromakin.exceptions.ServerException;
import org.dromakin.server.Server;
import org.dromakin.server.request.RequestMethod;
import org.dromakin.server.response.Code;
import org.dromakin.server.response.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.setupServer();

            for (String s : validPaths) {
                server.addHandler(RequestMethod.GET, s, (request, out) -> {
                    try {
                        final var filePath = Paths.get(Server.getPathToStaticFiles().toString(), request.getUrl());
                        final var contentType = Files.probeContentType(filePath);

                        if (request.getUrl().equals("/classic.html")) {

                            final var template = Files.readString(filePath);
                            final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
                            out.write((
                                    (
                                            Response.builder()
                                                    .code(Code.OK)
                                                    .contentType(contentType)
                                                    .contentLength((long) content.length)
                                                    .build()
                                    )
                                            .getResponse()
                            ).getBytes());

                            out.write(content);

                        } else {

                            final var contentLength = Files.size(filePath);
                            out.write((
                                    (
                                            Response.builder()
                                                    .code(Code.OK)
                                                    .contentType(contentType)
                                                    .contentLength(contentLength)
                                                    .build()
                                    )
                                            .getResponse()
                            ).getBytes());
                            Files.copy(filePath, out);

                        }

                        out.flush();

                    } catch (IOException e) {
                        if (e.getCause() != null)
                            e.getCause().printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }

            server.start();

        } catch (ServerException e) {
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            throw new RuntimeException(e);
        }
    }
}


