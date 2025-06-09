fn main() {
    // 确保Android Java文件在适当的时候被编译
    #[cfg(target_os = "android")]
    {
        println!("cargo:rustc-link-lib=dylib=jni");
    }
    
    tauri_build::build()
}
