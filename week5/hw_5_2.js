use test
db.zips.aggregate([ {$match:{state:{$in:['CT','NJ']}}},
                    {$group:{_id:{state:"$state",city:"$city"}, cnt:{$sum:1}, popsum:{$sum:"$pop"}}}, 
                    {$match:{popsum:{$gt:25000}}}, 
                    {$group:{_id:null, popavg:{$avg:"$popsum"}}}
                    ])

