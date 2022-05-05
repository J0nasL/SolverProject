//remove this entire file if not using jfx
//also remove jfx as a dependency
module GUI {
    requires org.jetbrains.annotations;
    requires java.net.http;
    requires org.json;
    requires transitive javafx.controls;
    exports GUI;
}