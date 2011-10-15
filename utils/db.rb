require 'active_record'
require './address.rb'
ActiveRecord::Base.establish_connection(
    :adapter  => 'sqlite3',
    :database => '/home/alhazred/bitten.sqlite')

