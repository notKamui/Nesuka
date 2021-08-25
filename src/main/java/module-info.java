module com.notkamui.nesuka {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires kotlinx.coroutines.core.jvm;

    opens com.notkamui.nesuka to javafx.fxml;
    exports com.notkamui.nesuka;
}