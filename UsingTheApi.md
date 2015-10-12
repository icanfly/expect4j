# Code Style #

Firstly, writing expect-like in Java is a bit ugly. The language isn't blessed with closures like Tcl, and so there is a lot of boilerplate-like code around each match statement. The API tries to offer simplified methods for simple cases, for example:

instance.expect("login: ");

instead of:

expect.expect( new Match[.md](.md) {
> new GlobMatch("login: ", new Closure() {
> > public void run(ExpectState state) {
> > > state.exp\_continue();

> > }

> }
});

Secondly, when used in Tcl the api is identical to the C-based expect library.

# Examples #

(I hate when I see this in documentation:) The tests offer extensive example on writing different expect statements.