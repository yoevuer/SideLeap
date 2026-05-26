package hunoia.luno.service

interface SideGestureRuntime {
    fun nowInLauncher(): Boolean
    fun requestEnableFrozenPackage(packageName: String, onResult: (Boolean) -> Unit)
}
