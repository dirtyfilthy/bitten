require 'csv'
require "rubygems"
require "java"
require "/home/alhazred/bitten.jar"
java_import com.google.bitcoin.core.GraphBlockStore
java_import "net.dirtyfilthy.Bitten.Bitten"
store=GraphBlockStore.new(Bitten.networkParameters,"/home/alhazred/bitten.graph");
CSV.foreach('./addresses.txt') do |row|
  a=store.findOrCreateAddress(row[0])
  a.label=row[1]
  a.notes=row[2]
  a.save(store.graph)
  puts "#{row[0]} #{a.label} #{a.notes}"
  end
