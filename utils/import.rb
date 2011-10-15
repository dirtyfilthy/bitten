require 'csv'
require './db.rb'
CSV.foreach('./addresses.txt') do |row|
  a=Address.find_or_create_by_base58hash(row[0])
  a.label=row[1]
  a.notes=row[2]
  a.save
  puts "#{a.base58hash} #{a.label} #{a.notes}"
  end
