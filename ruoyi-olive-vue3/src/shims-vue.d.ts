// 通过使用类型声明文件，你可以确保在 TypeScript 项目中引入这些模块时，编译器能正确地进行类型检查和代码提示。这有助于提高代码质量和减少潜在的错误。

declare module "*.svg";
declare module "*.png";
declare module "*.jpg";
declare module "*.jpeg";
declare module "*.gif";
declare module "*.bmp";
declare module "*.tiff";


declare module "file-saver";
declare module "js-cookie";

// 声明一个模块，用于匹配所有以 ".vue" 结尾的文件
declare module "*.vue" {
    // 从 "vue" 中导入 DefineComponent 类型
    import { DefineComponent } from "vue";

    // 定义一个类型为 DefineComponent 的变量 component
    // 它具有三个泛型参数，分别表示组件的 props、组件的 data 和其他的类型。
    // 在这里，我们使用空对象（{}）表示没有 props，使用空对象（{}）表示没有 data，使用 any 表示其他类型可以是任意值。
    const component: DefineComponent<{}, {}, any>;

    // 导出 component 变量，这样其他地方在导入 ".vue" 文件时，TypeScript 编译器会将它识别为一个 Vue 组件
    export default component;
}

declare module 'particles.vue3';
declare module 'jsencrypt/bin/jsencrypt.min';
