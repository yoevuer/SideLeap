package hunoia.luno;

interface IShizukuCommandService {
    String listDisabledPackages();
    List<String> listDisabledPackageNames();
    String enablePackage(String packageName);
    String disablePackage(String packageName);
    String enablePackageApi(String packageName);
    String disablePackageApi(String packageName);
    String executeShellCommand(String command);
    void destroy();
}
