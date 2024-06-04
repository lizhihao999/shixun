layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery;

    form.on('submit(Add-filter)', function (data) {
        $.ajax({
            url: web.rootPath() + "visit_info/save",
            type: "post",
            contentType: "application/json",
            data: JSON.stringify(data.field),
            dataType: 'json',
            traditional: true,
            success: function (data) {
                layer.msg("操作成功", {
                    icon: 1,
                    success: function () {
                        reloadTb("Save-frame", "#SearchBtn");
                    }
                });
            },
            error: function (e) {
                layer.msg(e.responseJSON.message, {icon: 2});
            }

        });
        return false;
    });

    form.on('select(customerSelect)', function (data) {
        $.ajax({
            url: web.rootPath() + "linkman/listByCustomerId",
            type: "get",
            contentType: "application/json",
            data: {"custId":data.value},
            // data: {"custId":"9e5c2210b4ad3783953d8890ca0e3c4c"},
            dataType: 'json',
            traditional: true,
            success: function (data) {
                console.log(JSON.stringify(data));

                $("#linkmanId").empty();

                var openHtml = `<option value="">请选择</option>`
                if (data.data.length>0){
                    data.data.forEach(cust=>{
                        openHtml+= `<option value="${cust.custId}">${cust.linkman}</option>`
                    })
                }

                $("#linkmanId").html(openHtml);
                form.render('select','component-form-element')
            },
            error: function (e) {
                console.log(data.value)
                layer.msg(e.responseJSON.message, {icon: 2});
            }

        });
        return false;
    });

});
