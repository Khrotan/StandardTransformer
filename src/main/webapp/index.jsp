<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.4/jquery.min.js"></script>

<link rel="stylesheet" href="WEB-INF/prismjs/themes/prism.css">
<script src="WEB-INF/prismjs/components/prism-markup.min.js"></script>

<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
<script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>

<html>
<body>
<div class="col-xs-3">
    <h2 class="page-header">Option Side</h2>
    <div class="form-group">
        <label for="sel1">Source:</label>
        <select class="form-control" id="sel1">
            <option>EDXL-CAP</option>
            <option>EDXL-RM</option>
            <option>EDXL-TEP</option>
            <option>EDXL-SitRep</option>
            <option>EDXL-HAVE</option>
            <option>SensorML</option>
            <option>Observations & Measurements</option>
        </select>
    </div>
    <div class="form-group">
        <label for="sel1">Target:</label>
        <select class="form-control" id="sel2">
            <option>EDXL-RM</option>
            <option>EDXL-CAP</option>
            <option>EDXL-TEP</option>
            <option>EDXL-SitRep</option>
            <option>EDXL-HAVE</option>
            <option>SensorML</option>
            <option>Observations & Measurements</option>
        </select>
    </div>
    <button type="submit" class="btn btn-primary col-xs-12">Transform</button>
</div>
<div class="col-xs-9">
    <h2 class="page-header">Text Side</h2>
    <label for="exampleTextarea">Example textarea</label>
    <textarea class="form-control" style="height: 50%" id="exampleTextarea" rows="3"></textarea>
</div>
</body>
</html>
