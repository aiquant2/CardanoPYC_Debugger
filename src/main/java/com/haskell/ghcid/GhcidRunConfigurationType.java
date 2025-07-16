//package com.haskell.ghcid;
//
//
//import com.intellij.execution.configurations.ConfigurationFactory;
//import com.intellij.execution.configurations.ConfigurationType;
//import com.intellij.openapi.project.Project;
//import javax.swing.*;
//import org.jetbrains.annotations.NotNull;
//
//public class GhcidRunConfigurationType implements ConfigurationType {
//
//    private final ConfigurationFactory factory;
//
//    public GhcidRunConfigurationType() {
//        this.factory = new GhcidConfigurationFactory(this);
//    }
//
//    @Override
//    public String getDisplayName() {
//        return "Haskell GHCI Debug";
//    }
//
//    @Override
//    public String getConfigurationTypeDescription() {
//        return "Run GHCI via ghcid";
//    }
//
//    @Override
//    public Icon getIcon() {
//        return null; // Replace with icon if desired
//    }
//
//    @NotNull
//    @Override
//    public String getId() {
//        return "HASKELL_GHCID_RUN_CONFIG";
//    }
//
//    @Override
//    public ConfigurationFactory[] getConfigurationFactories() {
//        return new ConfigurationFactory[]{factory};
//    }
//}





// new

package com.haskell.ghcid;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;



public class GhcidRunConfigurationType extends ConfigurationTypeBase {

    public GhcidRunConfigurationType() {
        super("GHCID_RUN",
                "Ghcid",                              // Display Name
                "Run GHCi with ghcid integration",    // Description
                (Icon) null);
        System.out.println("runnnn 1");// Icon (optional)

        addFactory(new ConfigurationFactory(this) {
            @Override
            public @NotNull GhcidRunConfiguration createTemplateConfiguration(@NotNull Project project) {
                System.out.println("runnn  2");
                return new GhcidRunConfiguration(project, this, " Ghcid Auto run");
            }

            @Override
            public boolean isConfigurationSingletonByDefault() {
                System.out.println("runnn  3");
                return true;
            }
        });
    }

    // ✅ Static method to fetch instance via EP (Extension Point)
    public static GhcidRunConfigurationType getInstance() {
        ExtensionPointName<ConfigurationType> EP_NAME =
                ExtensionPointName.create("com.intellij.runConfigurationType");

        System.out.println("run ghcid");

        return EP_NAME.getExtensionList().stream()
                .filter(type -> type instanceof GhcidRunConfigurationType)
                .map(type -> (GhcidRunConfigurationType) type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("GhcidRunConfigurationType not registered."));
    }
}
