package hunoia.luno;

interface IShizukuCommandService {
    String executeShellCommand(String command);
    String enablePackage(String packageName);
    String disablePackage(String packageName);
    String enablePackageApi(String packageName);
    String disablePackageApi(String packageName);
    List<String> disablePackages(in List<String> packageNames);
    List<String> enablePackages(in List<String> packageNames);
    List<String> listDisabledPackageNames();
    void destroy();
}
