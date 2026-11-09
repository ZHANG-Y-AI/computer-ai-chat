# Go、Rust、Kotlin 等语言学习路线（入门到进阶）

## 一、Go 语言

### 入门
- 安装 Go toolchain，理解 GOPATH/GOMOD 与现代模块 `go mod`。
- 基础语法：变量、控制流、切片与 map、函数多返回值、结构体与方法。
- 接口（interface）与组合优于继承的惯用法。
- 错误处理：显式 `error` 返回值，defer 资源释放。

### 进阶
- Goroutine 与 channel，select、context 取消与超时。
- 标准库：net/http 搭建服务、encoding/json、testing 包写表驱动测试。
- 性能与诊断：pprof、race detector。

### 实战方向
- 云原生与微服务：gRPC、Kubernetes 客户端、简单 API 网关或配置中心侧车。
- CLI 工具：cobra、viper；高并发爬虫或日志聚合小服务。

## 二、Rust 语言

### 入门
- 所有权、借用、生命周期（先建立心智模型，再大量写小程序）。
- 模式匹配、enum、Option/Result 链式处理错误。
- Cargo 项目管理、单元测试与文档注释。

### 进阶
- 智能指针（Box、Rc、Arc）、并发与 Send/Sync。
- 异步生态：tokio 基础；与 C 互操作（FFI）概念。

### 实战方向
- 高性能 CLI、网络服务、嵌入式与 WASM；参与开源 crates 的 issue 修复。

## 三、Kotlin

### 入门
- 与 Java 互操作、空安全、数据类、扩展函数。
- 集合与 lambda、协程入门（suspend、launch、async）。

### 进阶
- Android Jetpack 组件或 Ktor 服务端；多平台（KMP）可按兴趣拓展。

### 实战方向
- Android 应用；Spring Boot 与 Kotlin 结合的后端项目。

## 四、Swift（Apple 生态）

### 入门
- 常值与类型推断、可选类型、guard、闭包。
- SwiftUI 或 UIKit 二选一做第一个 iOS/macOS 小应用。

### 进阶
- Combine/async-await、内存与性能调试、App 生命周期与沙盒。

## 五、SQL（与具体数据库结合）

### 入门
- SELECT/WHERE/JOIN/GROUP BY/HAVING，索引与执行计划粗读。
### 进阶
- 事务隔离级别、锁与死锁、慢查询优化、分页深翻页问题。

## 六、语言选择建议

- **后端高并发与运维友好**：Go。
- **内存安全与极致性能**：Rust（学习曲线陡，适合有毅力的学习者）。
- **Android 与现代 JVM 栈**：Kotlin。
- **苹果生态客户端**：Swift。
