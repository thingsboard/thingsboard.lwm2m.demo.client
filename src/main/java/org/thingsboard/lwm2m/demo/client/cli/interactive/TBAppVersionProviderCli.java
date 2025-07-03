package org.thingsboard.lwm2m.demo.client.cli.interactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class TBAppVersionProviderCli implements CommandLine.IVersionProvider {

    private final TBAppInfoProperties info;

    @Autowired
    public TBAppVersionProviderCli(TBAppInfoProperties info) {
        this.info = info;
    }

    @Override
    public String[] getVersion() {
        return new String[] {
                "Name " + info.getTitle(),
                "Version " + info.getVersion(),
                "BuildTime " + info.getBuild().getTime()
        };
    }
}
