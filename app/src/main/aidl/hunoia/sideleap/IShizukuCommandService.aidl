package hunoia.sideleap;

interface IShizukuCommandService {
    String listDisabledPackages();
    List<String> listDisabledPackageNames();
    String enablePackage(String packageName);
    String disablePackage(String packageName);
    String enablePackageApi(String packageName);
    String disablePackageApi(String packageName);
    void destroy();
}
