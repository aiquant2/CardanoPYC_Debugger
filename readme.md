# CardanoPyC_Debugger Pycharm  Plugin
![Version](https://img.shields.io/jetbrains/plugin/v/aiquant.plugins)
![Downloads](https://img.shields.io/jetbrains/plugin/d/aiquant.plugins)
![Rating](https://img.shields.io/jetbrains/plugin/r/stars/aiquant.plugins)


![IntelliJ Platform Plugin Template][file:intellij-platform-plugin-template-dark]
![CardanoPyC Plugin][file:intellij-platform-plugin-template-light]

[![official JetBrains project](https://jb.gg/badges/official.svg)][jb:github]
[![Twitter Follow](https://img.shields.io/badge/follow-%40JBPlatform-1DA1F2?logo=twitter)](https://twitter.com/JBPlatform)
[![Build](https://github.com/JetBrains/intellij-platform-plugin-template/workflows/Build/badge.svg)][gh:build]
[![Slack](https://img.shields.io/badge/Slack-%23intellij--platform-blue?style=flat-square&logo=Slack)](https://plugins.jetbrains.com/slack)

---
<!-- Plugin description -->
CardanoPyC is a powerful IntelliJ-based plugin that provides seamless support for Haskell and Plutus development . It enhances developer productivity by offering:

- **Diagnostics :** highlight error on editor  and show proper suggestion  for Haskell files (.hs)
- **Debug Tools :** on debug console show error if code is not correct 



This plugin is perfect for developers building on the Cardano blockchain, enabling smooth and efficient smart contract development within the IntelliJ ecosystem.
<!-- Plugin description end -->

---

## 📥 Installation

1. Clone the repository:
   ```
   git clone https://github.com/AIQUANT-Tech/CardanoPyC_Debugger
   cd CardanoPyC_Debugger

2. Debug the extension by clicking the Intellij IDE debug icon.
3. Build the plugin:
```
   ./gradlew buildPlugin
```

## 🏗️ Development

### ✅ Running Tests


![Run Test](images/RunTest.png)


## 🚀 Features


### Diagnostics
- Highlights errors directly in the editor for Haskell files (.hs)
- Provides intelligent suggestions for fixing issues

### Debug Tools
- Displays compilation/runtime errors in the Debug Console
- Helps developers quickly identify and resolve problems

## 📋 Prerequisites

- For diagnostic → ghcid must be installed .


## Development Environment

- IntelliJ IDEA version 231.x or higher

- Java 17+ runtime environment

- Minimum 4GB RAM recommended
## 🏗️ Project Structure
```  
CardanoPyC_Debugger
   └──src
       ├── main
       │   ├── java
       │   │       └── com
       │   │       ├── debug_tools
       │   │       └── diagnostics
       │   └── resources
       │       ├── colors
       │       ├── icons
       │       └── META-INF
       └── test
            └── java
                 └── com
                      ├── debug_tools
                      ├── diagnostics
                      └── haskell
                       

```

## 🛠️ Build Configuration
The project uses Gradle with the IntelliJ Platform Plugin. Key configuration files:
- `build.gradle.kts` - Gradle build configuration
- `gradle.properties` - Project properties and versions
- `plugin.xml` - Plugin manifest and extension points

## 🎨 Icons
- Custom icons located in `/icons/` directory


## 🤝 Contributing

We welcome contributions! Please feel free to submit pull requests or open issues for bugs and feature requests.

## 🆘 Support

For issues or questions related to the CardanoPyC plugin, please contact AIQUANT TECHNOLOGIES support through the plugin's GitHub repository.

## 📄 License

This project is licensed under the Apache License - see the LICENSE file for details.

---

*Note: This plugin requires IntelliJ IDEA version 231.x or higher and Java 17+ for optimal performance.*

[//]: # (Keep the existing links section from the original template)
[docs]: https://plugins.jetbrains.com/docs/intellij?from=IJPluginTemplate
[gh:build]: https://github.com/JetBrains/intellij-platform-plugin-template/actions?query=workflow%3ABuild
[jb:github]: https://github.com/JetBrains/.github/blob/main/profile/README.md
[file:intellij-platform-plugin-template-dark]: ./.github/readme/intellij-platform-plugin-template-dark.svg#gh-dark-mode-only
[file:intellij-platform-plugin-template-light]: ./.github/readme/intellij-platform-plugin-template-light.svg#gh-light-mode-only