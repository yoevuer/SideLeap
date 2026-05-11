package hunoia.sideleap;

interface IShizukuCommandService {
    String listDisabledPackages();
    List<String> listDisabledPackageNames();
    String enablePackage(String packageName);
    void destroy();
}