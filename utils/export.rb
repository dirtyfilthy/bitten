require 'csv'
require './db.rb'
res = CSV.generate do |csv|
  Address.find(:all,:conditions=>'label!=""').each do |a|
    csv << [a.base58hash,a.label,a.notes]
  end
end
puts res
