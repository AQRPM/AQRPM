extern crate aqrpm;
extern crate gtk;

use gtk::{
    Align,
    Box as GtkBox,
    Button,
    ButtonsType,
    ContainerExt,
    GtkWindowExt,
    Label,
    Menu,
    MenuItem,
    MessageDialog,
    RadioButton,
    WidgetExt,
    Window,
    WindowType
};

fn main() {
	if let Err(err) = gtk::init() {
		eprintln!("gtk error: {}", err);
		return;
	}

    println!("Hello, world!");

	let window = Window::new(WindowType::Toplevel);
	window.set_title("AQRPM - Vault");
	window.set_default_size(1152, 648);
    window.set_border_width(10);
    window.set_position(gtk::WindowPosition::Center);

    let button = gtk::Button::new_with_label("i am a giant temporary button, bitch!");
    //let label = gtk::Label::new("watch out for that button!");

    window.add(&button);
    //window.add(&label);

	window.show_all();

	gtk::main();
}
