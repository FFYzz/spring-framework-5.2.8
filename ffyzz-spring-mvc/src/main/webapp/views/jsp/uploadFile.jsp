<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<html>
<head>
    <title>上传文件</title>
</head>
<body>
<h1>上传文件简单 demo</h1>
<form method="POST" action="/file" enctype="multipart/form-data">
    <input type="file" name="files"/><br/><br/>
    <input type="file" name="files"/><br/><br/>
    <input type="submit" value="Submit"/>
</form>
<br>


</body>
</html>