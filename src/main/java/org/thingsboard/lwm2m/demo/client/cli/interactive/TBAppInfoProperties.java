package org.thingsboard.lwm2m.demo.client.cli.interactive;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class TBAppInfoProperties {
    // гетери/сетери
    private String version;
    private String title;
    private Build build = new Build();

    public static class Build {
        private String time;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public void setVersion(String version) { this.version = version; }

    public void setTitle(String title) { this.title = title; }

    public void setBuild(Build build) { this.build = build; }
}
