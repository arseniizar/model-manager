# Script Manager with Model Integration

This project is a Java-based GUI application designed for managing and executing scripts and models. Built using **Swing
**, **RSyntaxTextArea** for syntax highlighting, and **FlatLaf** for modern UI themes, the application allows users to:

- Select and view scripts.
- Write and run custom scripts.
- Execute chosen scripts on a model.
- View output and results in a structured format.

## Features

- **Script Management**:
    - View and edit chosen scripts.
    - Write new custom scripts with syntax highlighting.
    - Run scripts with a single click.

- **Model Integration**:
    - Choose a model to bind scripts and data.
    - View results in a tabular format.

- **UI Highlights**:
    - **FlatLaf Dark Theme** with SVG icons.
    - Syntax highlighting for Groovy and Java using **RSyntaxTextArea**.
    - Responsive panels with split layouts for better user experience.

## Project Structure

### Key Packages

- **`controller`**:
    - Handles the interaction between scripts, data, and the model.
    - Provides utilities for running scripts and models.

- **`ui`**:
    - Contains reusable components for the GUI.
    - Split into subpackages for modularity:
        - `ui.scriptstab`: Handles the scripts tab layout and interactions.

- **`annotations`**:
    - Custom annotations like `@Bind` for defining bindable fields in models.

### Components

1. **Script List Panel**:
    - Displays available scripts with icons.
    - Select scripts to view or edit.

2. **Script Content Panel**:
    - Split into two sections:
        - Chosen script (view-only).
        - Writable script (editable).

3. **Script Output Panel**:
    - Displays output from script execution.

4. **Controller**:
    - Core logic for running scripts and interacting with models and data.

## Prerequisites

- **Java 11 or above**.
- Maven for dependency management.

### Maven Dependencies

The project uses the following dependencies:

- **FlatLaf**: For modern, flat UI themes.
- **RSyntaxTextArea**: For syntax highlighting.
- **Groovy**: For executing Groovy scripts.

Add the following to your `pom.xml`:

```xml
    <dependencies>
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.5</version>
        </dependency>
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf-extras</artifactId>
            <version>3.4</version>
        </dependency>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy</artifactId>
            <version>3.0.22</version>
        </dependency>
        <dependency>
            <groupId>com.fifesoft</groupId>
            <artifactId>rsyntaxtextarea</artifactId>
            <version>3.5.1</version>
        </dependency>
    </dependencies>
