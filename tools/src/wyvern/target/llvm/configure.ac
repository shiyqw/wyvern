AC_INIT(WyvernLLVM, 0.1)
AM_INIT_AUTOMAKE([1.11 no-define foreign])
AC_PROG_CXX
LT_INIT
AC_CANONICAL_HOST
case $host_os in
	darwin*)
		JAVA_HOME=$(/usr/libexec/java_home)
                CXXFLAGS="$CXXFLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin"
		;;
	linux*)
		CXXFLAGS="$CXXFLAGS -I$JAVA_HOME/include -I$JAVA_HOME/include/linux"
		;;
esac
AC_CONFIG_FILES([Makefile])
#PKG_CHECK_MODULES([DEPS], [])
AM_SILENT_RULES([yes])
AC_OUTPUT
