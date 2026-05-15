package hunoia.sideleap.service

interface SideGestureRuntime {
    fun nowInLauncher(): Boolean
    fun requestEnableFrozenPackage(packageName: String, onResult: (Boolean) -> Unit)
}
