use test
db.grades.aggregate([ {$unwind:"$scores"},
                      {$match:{'scores.type':{$in:['exam','homework']}}},
                      {$group:{_id:{classid:'$class_id', studentid:'$student_id'}, 
                               gpa:{$avg:'$scores.score'}}},
                      {$group:{_id:{class:'$_id.classid'} ,avg_class:{$avg:'$gpa'}}},
                      {$sort:{avg_class:-1}} 
		    ])
