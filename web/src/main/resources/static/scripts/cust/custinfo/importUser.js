var use = layui.use(['upload'], function () {
    var $ = layui.jquery
        , upload = layui.upload;

    $("#templateHandle").on("click", function () {
        window.location.href = web.rootPath() + "custinfo/template";
    });

    //拖拽上传
    upload.render({
        elem: '#importUser-upload-drag'
        , url: web.rootPath() + 'custinfo/import'
        , accept: 'file'
        , auto: false
        , exts: 'xls|xlsx'
        , bindAction: '#uploadBtn'
        , done: function (res) {
            if (res.errCode == 0) {
                layer.msg("操作成功", {
                    icon: 1,
                    end: function () {
                        reloadTb("visitImport-frame", "#SearchBtn");
                    }
                });
            } else {
                layer.msg(e.responseJSON,message,{icon:2});
            }
        }
        , error: function (index, upload) {
            layer.msg("导入失败", {icon: 2});
        }
    });
});