package com.ngxqt.classmanagementmvvm.data.model

data class ClassItem(
    var cid: String? = null,
    var className: String = "",
    var subjectName: String = "",
    var status:String = ""
) {
    // Thêm constructor không đối số
    constructor() : this(null, "", "", "")
}

